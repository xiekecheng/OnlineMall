package com.changgou.goods.service.impl;

import com.changgou.goods.dao.AlbumMapper;
import com.changgou.goods.pojo.Album;
import com.changgou.goods.service.AlbumService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {
    @Autowired
    AlbumMapper albumMapper;
    @Override
    public PageInfo<Album> findPage(Album album, int page, int size) {
        //分页
        PageHelper.startPage(page,size);
        Example example = getExample(album);
        return new PageInfo<>(albumMapper.selectByExample(example));
    }

    @Override
    public PageInfo<Album> findPage(int page, int size) {
        return null;
    }

    @Override
    public List<Album> findList(Album album) {
        Example example = getExample(album);
        return albumMapper.selectByExample(example);
    }

    private Example getExample(Album album) {
        //创建查询 SELECT * FROM ALBUM
        Example example = new Example(Album.class);
        Example.Criteria criteria = example.createCriteria();

        //拼接条件
        if (album !=null){
            // 编号
            if(!StringUtils.isEmpty(album.getId())){
                criteria.andEqualTo("id",album.getId());
            }
            // 相册名称
            if(!StringUtils.isEmpty(album.getTitle())){
                criteria.andLike("title","%"+album.getTitle()+"%");
            }
            // 相册封面
            if(!StringUtils.isEmpty(album.getImage())){
                criteria.andEqualTo("image",album.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(album.getImageItems())){
                criteria.andEqualTo("imageItems",album.getImageItems());
            }
        }
        return example;
    }

    @Override
    public void delete(Long id) {
        albumMapper.deleteByPrimaryKey(id);

    }

    @Override
    public void update(Album album) {
        albumMapper.updateByPrimaryKeySelective(album);
    }

    @Override
    public void add(Album album) {
        albumMapper.insertSelective(album);

    }

    @Override
    public Album findById(Long id) {
        return albumMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Album> findAll() {
        return albumMapper.selectAll();
    }
}
